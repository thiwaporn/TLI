package infoasset.utils;

import javax.naming.NamingException;

public class TestLDAP {

   public static void main(String[] args) {
      try {
         System.out.println(LDAPTool.newInstance("panida.mun"));         
         System.out.println(LDAPTool.newInstance("manisa"));         
         System.out.println(LDAPTool.newInstanceById("9004209"));
         System.out.println(LDAPTool.newInstanceById("9005289"));
      } catch (NamingException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      
   }
   
}
