package mytools;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Filereader {
    BufferedReader in;
    public Filereader(String filename) throws FileNotFoundException {
        in=new BufferedReader(new FileReader(filename));
    }
    public int read() throws IOException {
        int x=0,f=1;char ch= (char) in.read();
        while(!isdigit(ch)){
            if(ch=='-'){
                f=-1;
            }
            ch= (char) in.read();
        }
        while(isdigit(ch)) {
            x=x*10+ch-'0';
            ch= (char) in.read();
        }
        return f*x;
    }

    private boolean isdigit(char ch) {
        return '0'<=ch&&ch<='9';
    }
}
