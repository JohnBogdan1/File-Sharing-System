
public class UserFile {

    // atributes
    String name;
    String ip;
    Integer port;
    String file_owner;
    String group_owned;
    boolean isOnline;

    public UserFile(String name, String ip, Integer port, String file_owner, String group_owned, Boolean isOnline) {
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.file_owner = file_owner;
        this.group_owned = group_owned;
        this.isOnline = isOnline;
    }

}
