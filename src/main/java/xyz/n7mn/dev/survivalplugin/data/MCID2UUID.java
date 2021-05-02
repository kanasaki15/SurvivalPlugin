package xyz.n7mn.dev.survivalplugin.data;

public class MCID2UUID {
    private String name;
    private String id;

    public MCID2UUID(String name, String id){
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }
}
