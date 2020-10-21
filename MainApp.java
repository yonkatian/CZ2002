import java.util.*;

public class MainApp {
    public static void main(Strings[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Please enter your username:");
        String userName = sc.nextLine();
        System.out.println("Please enter your password:"); // supposed to be censored/hashed
        String userPassword = sc.nextLine();
        while (true){
            if userName.toLowerCase() == CSV's userName.toLowerCase{
                if userPassword == CSV's userPassword{
                    if CSV's role == 0{
                        displayStudentMenu();
                        break;
                        }
                    else{
                        displayAdminMenu();
                        break;
                        }
                    }
                else{
                    System.out.println("Incorrect password!");
                    }
            else{
                System.out.println("Incorrect Username!");
                }
            }
        }
    }
}