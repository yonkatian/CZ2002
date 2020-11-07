public class MenuHandler {
    public static void printStudentMainMenu() {
        System.out.println("1. Add course");
        System.out.println("2. Drop course");
        System.out.println("3. Print courses registered");
        System.out.println("4. Check vacancies available");
        System.out.println("5. Change index number of course");
        //print out only students name, gender and nationality
        System.out.println("6. Swop index number with another");
        System.out.println("7. Quit");
    }
    public static void printAdminMainMenu() {
        System.out.println("1. Edit student access period");
        System.out.println("2. Add a student(name, metric number, gender, nationality, etc");
        System.out.println("3. Add a course (course code, school, its index numbers and vancancy");
        System.out.println("4. Check available slot for an index number (vacancy in a class)");
        System.out.println("5. Print student list by index number");
        //print out only students name, gender and nationality
        System.out.println("6. Print student list by course (all students registered for the selected course)");
        System.out.println("7. Quit");
    }
}