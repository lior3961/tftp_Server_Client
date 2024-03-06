package bgu.spl.net.srv;

import java.util.concurrent.ConcurrentHashMap;

public class ServerData {

    private ConcurrentHashMap<String, Boolean> users;
    
    public ServerData()
    {
        this.users = new ConcurrentHashMap<String, Boolean>();
    }

    public boolean logInOrRegister(String userName) //true if logged in, false if ERROR
    {
        if(this.users.containsKey(userName))
        {
            if(this.users.get(userName))
            {
                return false; //already logged in - ERROR

                // need to check if its firs action
                // */
            }
        }
        this.users.put(userName, true); //log in or register
        return true;
    }
    
}
