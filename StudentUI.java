import java.util.*;

public class StudentUI {
    public static void displayStudentMenu(){
        Scanner sc = new Scanner(System.in);
        System.out.print(
                "1. View courses by index\n" +
                "2. View courses by name\n" +
                "3. Check course vacancy\n" +
                "4. Drop course\n" +
                "5. Add course\n" +
                "6. Check/Print registered courses\n" +
                "7. Change index number of registered course\n" +
                "8. Swap index number with another student\n" +
                "9. View notifications\n" +
                "10. Exit Application\n");
        int userInput = sc.nextInt();
        switch (userInput) {
            case 1:
                System.out.println("View courses by index");
                // For loop to sort the array of courses by index then print it out
            case 2:
                System.out.println("View courses by name");
                // For loop to sort the array by alphabetical order then print it out
            case 3:
                System.out.println("Check course vacancy");
                System.out.println("Enter course number");
                int courseNumber = sc.nextInt();
                System.out.println("Enter index number");
                int indexNumber = sc.nextInt();
                CourseDetail.getVacancy(courseNumber, indexNumber); // For this method, i'm not sure if you want to make it a static method then put the course number as the argument to obtain
                // the vacancy for the object or not but we can edit this once we finalize
            case 4:
                System.out.println("Drop course");
                System.out.println("Enter course number");
                int courseNumber = sc.nextInt();
                Class.dropCourse(courseNumber);
                // Check if user has the course, if have, then drop course, if not then show error message that user does not have the course.
                // For this checking, i am not sure if we should place it in this class or put it in the method, but I think put in method better.
            case 5:
                System.out.println("Add course");
                System.out.println("Enter course number");
                int courseNumber = sc.nextInt();
                Class.addCourse(courseNumber);
                // Likewise for this method, check whether the user has reached the maximum number of AUs and whether it belongs to his school?
            case 6:
                System.out.println("Check/Print registered courses");
                // For loop and do System.out.println(CourseRegistered)
            case 7:
                System.out.println("Change index number of registered course");
                System.out.println("Enter course number");
                int courseNumber = sc.nextInt();
                System.out.println("Enter your current course index number");
                int currentIndexNumber = sc.nextInt();
                System.out.println("Enter your desired course index number");
                int desiredIndexNumber = sc.nextInt();
                int vacancy = CourseDetail.getVacancy(courseNumber, desiredIndexNumber);
                if (vacancy > 1) {
                    Course.swapIndex(currentIndexNumber, desiredIndexNumber);
                } else {
                    System.out.println("Sorry there are no vacant slots available.");
                }
            case 8:
                System.out.println("Swap index number with another student");
                System.out.println("Enter Course Number");
                courseNumber = sc.nextInt();
                System.out.println("Enter Course Index");
                indexNumber = sc.nextInt();
                System.out.println("Enter student's username:");
                String studentUsername = sc.nextLine();
                System.out.println("Enter student's password:"); // supposed to be censored/hashed
                String studentPassword = sc.nextLine();
                if studentUsername.toLowerCase() == CSV 's userName.toLowerCase{
                    if studentPassword == CSV 's userPassword{
                        // if student takes the same course, and have different index from user, then swap index
                        // else print a message and state reason why index cannot be swapped
                    else{
                        System.out.println("Incorrect password!");
                    }
                else{
                    System.out.println("Incorrect Username!");
                }
            case 9:
                System.out.printnln("View notifications");
                Notification.displayNotification();
            case 10:
                System.out.println("Exit Application");
                System.exit(0);
            default:
                System.out.println("Incorrect input, please try again");
        }
    }
}

