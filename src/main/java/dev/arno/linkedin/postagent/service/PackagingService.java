package dev.arno.linkedin.postagent.service;

public class PackagingService {
    public static int countChars(String s){
        return s.codePointCount(0, s.length());
    }
}
