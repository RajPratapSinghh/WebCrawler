package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;

public class Crawler {
    private HashSet<String> urlLink;
    private int MAX_DEPTH=2;
    private Connection connection;
    public Crawler(){
        connection= DatabaseConnection.getConnection();
        urlLink=new HashSet<String>();
    }
    public void getPageTextandLinks(String url,int depth){
        if(!urlLink.contains(url)){
            if(urlLink.add(url)){
                System.out.println(url);
            }
            try{
            Document document= Jsoup.connect(url).timeout(5000).get();
            String text= document.text().length()<501?document.text():document.text().substring(0,500);
            System.out.println(text);
                PreparedStatement preparedStatement= connection.prepareStatement("insert into pages values(?,?,?)");
                preparedStatement.setString(1,document.title());
                preparedStatement.setString(2,url);
                preparedStatement.setString(3,text);
                preparedStatement.executeUpdate();
            depth++;
            if(depth>MAX_DEPTH){
                return;
            }
                Elements availableLinksonPage = document.select("a[href]");
            for(Element currlink: availableLinksonPage){
                getPageTextandLinks(currlink.attr("abs:href"),depth);
            }
            }
            catch (IOException ioException){
                ioException.printStackTrace();
            }
            catch (SQLException s){
                s.printStackTrace();
            }
        }
    }
    public static void main(String[] args) {
        Crawler crawler=new Crawler();
        crawler.getPageTextandLinks("https://www.javatpoint.com", 0);
    }
}