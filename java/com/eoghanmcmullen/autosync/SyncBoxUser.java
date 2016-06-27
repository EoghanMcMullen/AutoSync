package com.eoghanmcmullen.autosync;

/**
 * Created by eoghanmcmullen on 12/04/2016.
 */

public class SyncBoxUser
{

    private String username;
    private String email;
    private String password;
    private String ip;
    private String ip_password;

    public SyncBoxUser(){}

    public SyncBoxUser(String username, String email, String password,String ip,String ip_password)
    {
        super();
        this.username = username;
        this.email = email;
        this.password = password;
        this.ip = ip;
        this.ip_password = ip_password;
    }

    //getters & setters

    @Override
    public String toString()
    {
        return "SyncBoxUser [username=" + username + ", email=" + email + ", password=" + password
                + ", ip=" + ip + ", ip_password=" + ip_password
                + "]";
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getUsername()
    {

        return username;
    }

    public String getEmail()
    {
        return email;
    }

    public String getPassword()
    {
        return password;
    }
    public String getIp()
    {
        return ip;
    }

    public void setIp(String ip)
    {
        this.ip = ip;
    }

    public String getIp_password()
    {
        return ip_password;
    }

    public void setIp_password(String ip_password)
    {
        this.ip_password = ip_password;
    }

}