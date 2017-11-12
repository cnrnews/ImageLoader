package imageloader.imooc.com.imageloader.adapter;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.List;

import imageloader.imooc.com.imageloader.R;
import imageloader.imooc.com.imageloader.entity.FolderBean;

/**
 * @author：lihl on 2017/11/11 21:12
 * @email：1601796593@qq.com
 */
public class ListImgDirAdapter extends CommonAdapter<FolderBean> {
    public ListImgDirAdapter(Context context, int layoutId, List<FolderBean> datas) {
        super(context, layoutId, datas);
    }
    @Override
    protected void convert(ViewHolder holder, FolderBean folderBean, int position) {

        holder.setText(R.id.tv_dir_item_name,folderBean.getName());
        holder.setText(R.id.tv_dir_item_count,folderBean.getCount()+"");
        Glide.with(mContext)
                .load(folderBean.getFirstImgPath())
                .placeholder(R.mipmap.pictures_no)
                .error(R.mipmap.pictures_no)
                .crossFade()
                .into( (ImageView) holder.getView(R.id.iv_dir_firstimg));
    }
}
