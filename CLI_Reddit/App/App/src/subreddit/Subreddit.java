package subreddit;

public class Subreddit {
    private String name;
    private String description;
    private String owner;

    public Subreddit(String name, String description, String owner){
        if(name == null || !name.startsWith("r/")) {
            this.name = "r/" + name;
        }
        else{
            this.name = name;
        }
        this.description = description;
        this.owner = owner;
    }

    public String getName(){
        return name;
    }
    public String getDescription(){
        return description;
    }
    public String getOwner(){ return owner; }

    public void setName(String name)
    {
        if(!name.startsWith("r/")) {
            name = "r/" + name;
        }
        this.name = name;
    }
    public void setDescription(String description){
        this.description = description;
    }
}
