package imageloader.imooc.com.imageloader.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import imageloader.imooc.com.imageloader.R;

/**
 * @author：lihl on 2017/11/7 22:57
 * @email：1601796593@qq.com
 */
public class ImageAdapter extends CommonAdapter<String>
{
    private String dirPath;

    private Set<String>mSelected=new HashSet<>();
    public ImageAdapter(Context context, int layoutId, List<String> datas,String dirPath) {
        super(context, layoutId, datas);
        this.dirPath=dirPath;
    }
    @Override
    protected void convert(ViewHolder holder, String path, int position) {

        final ImageView imageView=holder.getView(R.id.id_item_img);
        final ImageButton imgButton=holder.getView(R.id.id_item_selecte);
        holder.setImageResource(R.id.id_item_selecte,R.mipmap.icon_image_un_select);
        imageView.setColorFilter(null);

        Glide.with(mContext)
                .load(dirPath+"/"+path)
                .placeholder(R.mipmap.pictures_no)
                .error(R.mipmap.pictures_no)
                .crossFade()
                .into( (ImageView) holder.getView(R.id.id_item_img));

        final String filePath=dirPath+"/"+path;
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSelected.contains(filePath))
                {
                    mSelected.remove(filePath);
                    imageView.setColorFilter(null);
                    imgButton.setImageResource(R.mipmap.icon_image_un_select);
                }else{
                    mSelected.add(filePath);
                    imageView.setColorFilter(Color.parseColor("#77000000"));
                    imgButton.setImageResource(R.mipmap.icon_image_select);
                }
            }
        });
        if(mSelected.contains(filePath))
        {
            imgButton.setImageResource(R.mipmap.icon_image_select);
            imageView.setColorFilter(Color.parseColor("#77000000"));
        }
    }
}
