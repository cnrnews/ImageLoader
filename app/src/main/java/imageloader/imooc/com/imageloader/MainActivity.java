package imageloader.imooc.com.imageloader;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.fingerth.supdialogutils.SYSDiaLogUtils;
import com.joker.api.Permissions4M;
import com.joker.api.wrapper.ListenerWrapper;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import imageloader.imooc.com.imageloader.adapter.ImageAdapter;
import imageloader.imooc.com.imageloader.entity.FolderBean;
import imageloader.imooc.com.imageloader.popwindow.ListImgDirPopUpWindow;

/***
 * @author lihl
 */
public class MainActivity extends Activity {


    @BindView(R.id.recyclerview)
    RecyclerView recyclerview;
    @BindView(R.id.id_dir_name)
    TextView idDirName;
    @BindView(R.id.id_dir_count)
    TextView idDirCount;
    @BindView(R.id.id_bottom_ly)
    FrameLayout idBottomLy;


    private ListImgDirPopUpWindow listImgDirPopUpWindow;


    private List<String> mImgs;
    private File mCurrentDir;
    private int mMaxCount;
    private List<FolderBean> mFolders = new ArrayList<FolderBean>();
    private ImageAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initView();
        initEvent();
        initData();
    }

    private void initEvent() {
        idBottomLy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listImgDirPopUpWindow.setAnimationStyle(R.style.dir_popupwindow_anim);
                listImgDirPopUpWindow.showAsDropDown(idBottomLy,0,0);
                lightOff();
            }
        });


    }

    private void initView() {
        recyclerview.setLayoutManager(new GridLayoutManager(this, 3));
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            data2View();
            SYSDiaLogUtils.dismissProgress();
            initDirPopupWindow();
        }
    };

    private void initDirPopupWindow() {
       listImgDirPopUpWindow=new ListImgDirPopUpWindow(this,mFolders);
       listImgDirPopUpWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
           @Override
           public void onDismiss() {
               lightOn();
           }
       });
        listImgDirPopUpWindow.setListener(new ListImgDirPopUpWindow.onDirSelectedListener() {
            @Override
            public void onSelect(FolderBean folderBean) {
                listImgDirPopUpWindow.dismiss();

                mCurrentDir=new File(folderBean.getDir());
                mImgs=Arrays.asList(mCurrentDir.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File file, String s) {
                        return s.endsWith(".jpg") || s.endsWith(".jpeg") || s.endsWith(".png");
                    }
                }));
                adapter = new ImageAdapter(MainActivity.this, R.layout.item_img, mImgs, mCurrentDir.getAbsolutePath());
                recyclerview.setAdapter(adapter);

                idDirName.setText(mCurrentDir.getAbsolutePath());
                idDirCount.setText(mImgs.size()+"");


            }
        });
    }
    /**开灯效果*/
    private void lightOn() {
        WindowManager.LayoutParams lp=getWindow().getAttributes();
        lp.alpha=1.0f;
        getWindow().setAttributes(lp);
    }
    /**关灯效果*/
    private void lightOff() {
        WindowManager.LayoutParams lp=getWindow().getAttributes();
        lp.alpha=0.3f;
        getWindow().setAttributes(lp);
    }

    private void initData() {
        SYSDiaLogUtils.showProgressDialog(this, SYSDiaLogUtils.SYSDiaLogType.IosType, "加載中...", false,null);
        new Thread() {
            @Override
            public void run() {
                super.run();
                scanSDCardImgs();
                handler.sendEmptyMessage(0x110);

            }
        }.start();
    }

    public void data2View() {
        if (mCurrentDir == null) {
            Toast.makeText(this, " 未扫描到任何图片", Toast.LENGTH_SHORT).show();
            return;
        }
        mImgs = Arrays.asList(mCurrentDir.list());
        adapter = new ImageAdapter(this, R.layout.item_img, mImgs, mCurrentDir.getAbsolutePath());
        recyclerview.setAdapter(adapter);
        idDirName.setText(mCurrentDir.getAbsolutePath());
        idDirCount.setText(mImgs.size()+"");
    }

    /**
     * 扫描SDk卡图片
     */
    public void scanSDCardImgs() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "没有外部存储卡", Toast.LENGTH_SHORT).show();
            return;
        }
        Permissions4M.get(MainActivity.this)
                // 是否强制弹出权限申请对话框，建议设置为 true，默认为 true
                // .requestForce(true)
                // 是否支持 5.0 权限申请，默认为 false
                // .requestUnderM(false)
                // 权限，单权限申请仅只能填入一个
                .requestPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                // 权限码
                .requestCodes(0X110)
                .requestListener(new ListenerWrapper.PermissionRequestListener() {
                    @Override
                    public void permissionGranted(int i) {
                        progressScanImgs();
                    }

                    @Override
                    public void permissionDenied(int i) {

                    }

                    @Override
                    public void permissionRationale(int i) {

                    }
                })
                .request();


    }

    /**
     * 扫描SD卡逻辑
     */
    private void progressScanImgs() {
        Uri mImgUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver cr = MainActivity.this.getContentResolver();
        Cursor cursor = cr.query(mImgUri, null, MediaStore.Images.Media.MIME_TYPE + " =? or " + MediaStore.Images.Media.MIME_TYPE + " =? ", new String[]{"image/jpeg", "image/png"}, MediaStore.Images.Media.DATE_MODIFIED);
        Set<String> mDirPaths = new HashSet<>();
        while (cursor.moveToNext()) {
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            File parentFile = new File(path).getParentFile();
            if (parentFile == null) {
                continue;
            }
            String dirPath = parentFile.getAbsolutePath();
            FolderBean folderbean = null;
            if (mDirPaths.contains(dirPath)) {
                continue;
            } else {
                mDirPaths.add(dirPath);
                folderbean = new FolderBean();
                folderbean.setDir(dirPath);
                folderbean.setFirstImgPath(path);
            }
            if (parentFile.list() == null) {
                continue;
            }
            int count = parentFile.list(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    return s.endsWith(".jpg") || s.endsWith(".jpeg") || s.endsWith(".png");
                }
            }).length;
            folderbean.setCount(count);
            mFolders.add(folderbean);
            if (count > mMaxCount) {
                mMaxCount = count;
                mCurrentDir = parentFile;
            }
        }
        cursor.close();
    }

    @OnClick(R.id.id_bottom_ly)
    public void onViewClicked() {
        Toast.makeText(this, "click", Toast.LENGTH_SHORT).show();
    }
}
