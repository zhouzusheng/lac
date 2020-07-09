package com.aitek.common.lac;

import java.io.File;
import java.util.ArrayList;

public class BaiduLac {
    public static final String OSName = System.getProperty("os.name").toLowerCase();
    public static final boolean IsWindows = OSName.indexOf("windows") > -1;
    public static final boolean IsLinux = OSName.indexOf("linux") > -1;


    // 用于指向创建的LAC对象
    private long self_ptr;

    static {
        if(IsWindows) {
            loadLibraryForWin();
        } else if(IsLinux) {
            loadLibraryForLinux();
        } else {
            //do nothing
        }
    }

    private static void loadLibraryForWin() {
        String path = System.getProperty("lac_library_path", System.getProperty("user.dir"));
        File dir = new File(path);
        System.load(new File(dir, "native/baidu/openblas.dll").getAbsolutePath());
        System.load(new File(dir, "native/baidu/baidulac.dll").getAbsolutePath());
    }

    private static void loadLibraryForLinux() {
        String path = System.getProperty("lac_library_path", System.getProperty("user.dir"));
        File dir = new File(path);
        System.load(new File(dir, "native/baidu/libpaddle_fluid.so").getAbsolutePath());
        System.load(new File(dir, "native/baidu/libbaidulac.so").getAbsolutePath());
    }
    public BaiduLac(String model_dir) {
        init(model_dir);
    }
    public BaiduLac(BaiduLac model) {
        from(model.self_ptr);
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    public void close() {
        if(self_ptr != 0) {
            clear(self_ptr);
            self_ptr = 0;
        }
    }

    public boolean isClosed() {
        return self_ptr == 0;
    }

    // 装载model_path路径的模型
    private native void init(String model_path);
    private native void from(long self_ptr);
    private static native void clear(long self_ptr);

    // 装载dict_path路径的词典
    //注意：百度是从文本加载转换成 ahocorasick
    //他们没有优化，词个数太多时比较慢
    public native int loadCustomization(String dict_path);

    // 运行LAC，并将结果返回到words和tags中
    public native int run(String sentence, ArrayList<String> words, ArrayList<String> tags);

    /**
     * 添加的函数：运行LAC， 返回label， 调用者自己解释label 含义
     * 这时候词典不起作用！！！！将来我们自己干预
     * label 如下：
     * 0	a-B
     * 1	a-I
     * 2	ad-B
     * 3	ad-I
     * 4	an-B
     * 5	an-I
     * 6	c-B
     * 7	c-I
     * 8	d-B
     * 9	d-I
     * 10	f-B
     * 11	f-I
     * 12	m-B
     * 13	m-I
     * 14	n-B
     * 15	n-I
     * 16	nr-B
     * 17	nr-I
     * 18	ns-B
     * 19	ns-I
     * 20	nt-B
     * 21	nt-I
     * 22	nw-B
     * 23	nw-I
     * 24	nz-B
     * 25	nz-I
     * 26	p-B
     * 27	p-I
     * 28	q-B
     * 29	q-I
     * 30	r-B
     * 31	r-I
     * 32	s-B
     * 33	s-I
     * 34	t-B
     * 35	t-I
     * 36	u-B
     * 37	u-I
     * 38	v-B
     * 39	v-I
     * 40	vd-B
     * 41	vd-I
     * 42	vn-B
     * 43	vn-I
     * 44	w-B
     * 45	w-I
     * 46	xc-B
     * 47	xc-I
     * 16	PER-B
     * 17	PER-I
     * 18	LOC-B
     * 19	LOC-I
     * 20	ORG-B
     * 21	ORG-I
     * 34	TIME-B
     * 35	TIME-I
     * 48	O
     */
    public native int runLabels(String sentence, ArrayList<String> labels);

    public static void main(String[] args) {
        BaiduLac lac = new BaiduLac("my_seg_model");
        ArrayList<String> words = new ArrayList<>();
        ArrayList<String> tags = new ArrayList<>();

        lac.run("我们的国家", words, tags);
        System.out.println(words);
        System.out.println(tags);

        BaiduLac newLac = new BaiduLac(lac);

        lac.close();
        newLac.runLabels("我们的国家", tags);
        System.out.println(tags);
        newLac.close();
    }
}
