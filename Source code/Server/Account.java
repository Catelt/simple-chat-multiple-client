/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author hovan
 */
public class Account {
    String username;
    String password;
    
    Account(){
        username = "";
        password = "";
    }
    
    public void setUsername(String user){
        this.username = user;
    }
    
    public String getUsername(){
        return this.username;
    }
    
    public void setPassword(String pass){
        this.password = pass;
    }
    
    public String getPassword(){
        return this.password;
    }
}
