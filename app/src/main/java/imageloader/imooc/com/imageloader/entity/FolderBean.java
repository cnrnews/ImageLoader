package imageloader.imooc.com.imageloader.entity;

/**
 * 文件
 *
 * @author：lihl on 2017/11/7 20:30
 * @email：1601796593@qq.com
 */
public class FolderBean {

    /**
     * 文件夹路径
     */
    private String dir;
    /**
     * 第一张图片路径
     */
    private String firstImgPath;
    private String name;
    private int count;

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;


        int lastIndex = dir.lastIndexOf("/");
        name = dir.substring(lastIndex);

    }

    public String getFirstImgPath() {
        return firstImgPath;
    }

    public void setFirstImgPath(String firstImgPath) {
        this.firstImgPath = firstImgPath;
    }

    public String getName() {
        return name;
    }


    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
