package imageloader.imooc.com.imageloader;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * @author：lihl on 2017/11/5 17:09
 * @email：1601796593@qq.com
 */
public class ImageLoader {

    private static ImageLoader imageLoader;
    /**
     * 图片缓存
     */
    private LruCache<String, Bitmap> mLruCache;
    /***线程池*/
    private ExecutorService mThreadPool;
    private static final int DEFAULT_THREAD_COUNT = 1;
    /**
     * 调度策略
     */
    private Type mType = Type.LIFO;
    private LinkedList<Runnable> mTaskQueue;
    /**
     * 轮询线程
     */
    private Thread mPoolThread;
    private Handler mPollThreadHandler;
    /**
     * UI线程Handler
     */
    private Handler mUIHandler;

    private Semaphore mSemaphorePollThreadHandler = new Semaphore(0);

    private Semaphore mSemaphorePoll;

    /**
     * 调度策略
     */
    public enum Type {
        FIFO, LIFO
    }
    private ImageLoader() {
    }
    private ImageLoader(int threadCount, Type type) {
        init(threadCount, type);
    }
    private void init(int threadCount, Type type) {

        //后台轮询线程
        mPoolThread = new Thread() {
            @Override
            public void run() {
                super.run();
                Looper.prepare();
                mPollThreadHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        //线程池取 任务执行
                        mThreadPool.execute(getTask());
                        try {
                            mSemaphorePoll.acquire();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                //释放信号量
                mSemaphorePollThreadHandler.release();
                Looper.loop();
            }
        };
        mPoolThread.start();
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheMemory = maxMemory / 8;
        mLruCache = new LruCache<String, Bitmap>(cacheMemory) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
        mThreadPool = Executors.newFixedThreadPool(threadCount);
        mTaskQueue = new LinkedList<>();
        mType = type;

        mSemaphorePoll = new Semaphore(threadCount);
    }

    private Runnable getTask() {
        if (mType == Type.FIFO) {
            return mTaskQueue.removeFirst();
        } else if (mType == Type.LIFO) {
            return mTaskQueue.removeLast();
        }
        return null;
    }

    public static ImageLoader getInstance(int threadCount,Type type) {
        if (imageLoader == null) {
            synchronized (ImageLoader.class) {
                if (imageLoader == null) {
                    imageLoader = new ImageLoader(threadCount,type);
                }
            }
        }
        return imageLoader;
    }

    public void loadImage(final String path, final ImageView imageview) {
        imageview.setTag(path);
        if (mUIHandler == null) {
            mUIHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    //获取异步加载图片
                    ImageHolder holder = (ImageHolder) msg.obj;
                    Bitmap bm = holder.bitmap;
                    ImageView imageview = holder.imageview;
                    String path = holder.path;
                    //比较存储路径
                    if (imageview.getTag().equals(path)) {
                        imageview.setImageBitmap(bm);
                    }
                }
            };
            Bitmap bm = getBitmapFromCache(path);
            if (bm != null) {
                refreshBitmap(path, imageview, bm);
            } else {
                addTask(new Runnable() {
                    @Override
                    public void run() {
                        //加载图片,压缩
                        ImageSize imageSize = getImageViewSize(imageview);
                        Bitmap bm = decodeSampleBitmap(path, imageSize.width, imageSize.height);
                        //加入到缓存
                        addBitampTocache(path, bm);
                        refreshBitmap(path, imageview, bm);
                        mSemaphorePoll.release();
                    }
                });
            }
        }

    }

    private void refreshBitmap(String path, ImageView imageview, Bitmap bm) {
        Message msg = Message.obtain();
        ImageHolder holder = new ImageHolder();
        holder.bitmap = bm;
        holder.path = path;
        holder.imageview = imageview;
        msg.obj = holder;
        mUIHandler.sendMessage(msg);
    }

    private void addBitampTocache(String path, Bitmap bm) {
        if (getBitmapFromCache(path) == null) {
            if (bm != null) {
                mLruCache.put(path, bm);
            }
        }
    }

    /**
     * 图片压缩
     */
    private Bitmap decodeSampleBitmap(String path, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = cacaluteInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * 计算压缩比例
     */
    private int cacaluteInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int width = options.outWidth;
        int height = options.outHeight;
        int insampleSize = 1;
        if (width > reqWidth || height > reqHeight) {
            int widthRadio = Math.round(width * 1.0f / reqWidth);
            int heightRadio = Math.round(height * 1.0f / reqHeight);
            insampleSize = Math.max(widthRadio, heightRadio);
        }
        return insampleSize;
    }

    private ImageSize getImageViewSize(ImageView imageview) {
        ImageSize imageSize = new ImageSize();
        DisplayMetrics displayMetrics = imageview.getContext().getResources().getDisplayMetrics();
        ViewGroup.LayoutParams lp = imageview.getLayoutParams();
        int width = imageview.getWidth();
        if (width <= 0) {
            width = lp.width;
        }
        //最大值
        if (width <= 0) {
            width = getImageViewFieldValue(imageview,"mMaxWidth");
        }
        if (width <= 0) {
            width = displayMetrics.widthPixels;
        }

        int height = imageview.getHeight();
        if (height <= 0) {
            height = lp.height;
        }
        //最大值
        if (height <= 0) {
            height = getImageViewFieldValue(imageview,"mMaxHeight");
        }
        if (height <= 0) {
            height = displayMetrics.heightPixels;
        }

        imageSize.width = width;
        imageSize.height = height;

        return imageSize;
    }

    public static int getImageViewFieldValue(Object object, String fieldName) {
        int value = 0;
        try {
            Field field = ImageView.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            int fieldValue = field.getInt(object);
            if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
                value = fieldValue;
            }
        } catch (Exception e) {

        }
        return value;
    }
    private synchronized void addTask(Runnable runnable) {
        mTaskQueue.add(runnable);
        if (mPollThreadHandler == null) {
            try {
                mSemaphorePollThreadHandler.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mPollThreadHandler.sendEmptyMessage(0x110);
    }

    private Bitmap getBitmapFromCache(String path) {
        return mLruCache.get(path);
    }

    class ImageSize {
        public int width;
        public int height;
    }

    class ImageHolder {
        public Bitmap bitmap;
        public ImageView imageview;
        public String path;
    }

}
