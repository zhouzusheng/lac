package com.aitek.common.lac;

public enum Attribute {
    a("形容词", 1),
    ad("副形词", 2),
    an("名形词", 3),
    c("连词", 4),
    d("副词", 5),
    f("方位词", 6),
    m("数词", 7),
    n("名词", 8),
    nr("人名", 9),
    ns("地名", 10),
    nt("机构名", 11),
    nw("新词", 12),
    nz("专名", 13),
    p("介词", 14),
    q("量词", 15),
    mq("数量词", 16),
    r("代词", 17),
    s("处所词", 18),
    t("时间词", 19),
    u("助词", 20),
    v("动词", 21),
    vd("副动词", 22),
    vn("名动词", 23),
    w("标点符号", 24),
    xc("字符串", 25),
    PER("人名2",9),
    LOC("地名2",10),
    ORG("机构名2",11),
    TIME("时间词2",19),
    O("其他词",26),
    ;

    public static final int BASE = 256;

    // 成员变量
    private String desc;
    private int index;

    // 构造方法
    Attribute(String desc, int index) {
        this.desc = desc;
        this.index = index + BASE;
    }

    // 普通方法
    public static Attribute getByIndex(int index) {
        for (Attribute c : Attribute.values()) {
            if (c.getIndex() == index) {
                return c;
            }
        }
        return null;
    }

    public static Attribute getByDesc(String name) {
        for (Attribute c : Attribute.values()) {
            if (c.getDesc().equals(name)) {
                return c;
            }
        }
        return null;
    }

    // get方法
    public String getDesc() {
        return desc;
    }

    public int getIndex() {
        return index;
    }



}
