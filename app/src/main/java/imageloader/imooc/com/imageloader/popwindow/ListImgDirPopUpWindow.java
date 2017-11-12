package imageloader.imooc.com.imageloader.popwindow;


import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;

import java.util.List;

import imageloader.imooc.com.imageloader.R;
import imageloader.imooc.com.imageloader.adapter.ListImgDirAdapter;
import imageloader.imooc.com.imageloader.entity.FolderBean;

/**
 * @author：lihl on 2017/11/11 21:01
 * @email：1601796593@qq.com
 */
public class ListImgDirPopUpWindow extends PopupWindow implements MultiItemTypeAdapter.OnItemClickListener {

    private int mWidth;
    private int mHeight;
    private View mContentView;
    private RecyclerView recyclerView;
    private List<FolderBean> mDatas;

    private ListImgDirAdapter listImgDirAdapter;

    private onDirSelectedListener listener;

    public void setListener(onDirSelectedListener listener) {
        this.listener = listener;
    }

    public interface onDirSelectedListener{
        void onSelect(FolderBean folderBean);
    }

    public ListImgDirPopUpWindow(Context context, List<FolderBean> datas) {
        super(context);
        this.mDatas = datas;

        calWidthAndHeight(context);
        setWidth(mWidth);
        setHeight(mHeight);
        LayoutInflater layoutInflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContentView=layoutInflater.inflate(R.layout.popup_main,null);
        setContentView(mContentView);

        setFocusable(true);
        setOutsideTouchable(true);
        setTouchable(true);
        setBackgroundDrawable(new BitmapDrawable());

        setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction()==MotionEvent.ACTION_OUTSIDE)
                {
                    return  true;
                }
                return false;
            }
        });
        initView(context);
        initEvent();

    }
    private void initView(Context context) {
        recyclerView=mContentView.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false));
        listImgDirAdapter=new ListImgDirAdapter(context,R.layout.item_popup_main,mDatas);
        recyclerView.setAdapter(listImgDirAdapter);

    }
    private void initEvent() {
        listImgDirAdapter.setOnItemClickListener(this);
    }
    private void calWidthAndHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics matrix = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(matrix);
        mWidth = matrix.widthPixels;
        mHeight = (int) (matrix.heightPixels * 0.7f);
    }

    @Override
    public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {

        if (listener!=null)
        {
            listener.onSelect(mDatas.get(position));
        }

    }

    @Override
    public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
        return false;
    }
}
