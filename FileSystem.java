import java.util.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

//import javax.mail.*;

import models.Course;
import models.CourseTaken;
import models.Student;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger; 
import java.security.MessageDigest; 
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat; 

public class FileSystem{
    //array list of files for the current logged in user

    static ArrayList<Student> students = new ArrayList<Student>();
    static ArrayList<Course> courses = new ArrayList<Course>();
    static ArrayList<String> settings = new ArrayList<String>();
    static Student currentStudent;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        if(args.length == 1){
            String command = args[0];
            if(command.equals("-i")){
                System.out.print("Username: ");
                String username = sc.next();
                if(!exist(username)){
                    System.out.print("Password: ");
                    String password = sc.next();
                    System.out.print("Confirm Password: ");
                    String confirmPassword = sc.next();
                    if(confirmPassword.equals(password)){
                        if(isPasswordStrong(password)){
                            String salt = generateSalt();
                            writeToFile(username+":"+salt,"salt.txt");
                            String passSalt = password+salt;
                            String hash = toHash(passSalt);
                            writeToFile(username+":"+hash+":"+"A","shadow.txt");
                            System.out.println("Admin added!");
                            return;
                        } else {
                            System.out.println("Password not strong enough...");
                            System.out.println("Make sure password is greater than 8 characters long");
                        }
                    } else {
                        System.out.println("Passwords mismatch...");
                    }
                } else {
                    System.out.println("User exists!");
                }
            } else {
                System.out.println("Incorrect command");
            }
        } else {
            //Login
            System.out.print("Username: ");
            String username = sc.next();
            System.out.print("Password: ");
            String password = sc.next();
            String salt = getSalt(username);
            if(!salt.equals("")){
                String passSalt = password+salt;
                String hash = toHash(passSalt.trim());
                String hashFromShadow = getHashFromShadow(username);
                if(hash.trim().equals(hashFromShadow.trim())){
                    loadSettingsFromFile(); //read from settings.txt
                    loadStudentsFromFile(); //read from students.txt
                    loadCoursesFromFile(); //read from courses.txt
                    //user logged in
                    String userType = getUserTypeFromShadow(username);
                    if(userType.equals("S")){ //Student
                        //TODO: make sure student is able to log into system (check date)
                        if(checkAccessPeriod(settings.get(0), settings.get(1))){
                            //load current student
                            currentStudent = getStudentByUsername(username);
                            boolean quit = false;
                            while(!quit){
                                MenuHandler.printStudentMainMenu();
                                //load current student
                                int choice = sc.nextInt();
                                if(choice == 1){
                                    addStudentToCourse(sc);
                                }
                                if(choice == 2){
                                    dropStudentFromCourse(sc);
                                }
                                if(choice == 3){
                                    printCoursesRegistered();
                                }
                                if(choice == 4){
                                    checkVacanciesAvailable(sc);
                                }
                                if(choice == 5){
                                    changeIndexNumberOfCourse(sc);
                                }
                                if(choice == 6){
                                    swopIndexNumberWithAnother(sc);
                                }
                                if(choice == 7){
                                    quit = true;
                                }
                            }
                        } else {
                            System.out.println("Access period has closed");
                        }
                    }
                    if(userType.equals("A")){ //Admin
                        boolean quit = false;
                        while(!quit){
                            MenuHandler.printAdminMainMenu();
                            int choice = sc.nextInt();
                            if(choice == 1){
                                editStudentAccessPeriod(sc);
                            }
                            if(choice == 2){
                                addStudent(sc);
                            }
                            if(choice == 3){
                                addCourse(sc);
                            }
                            if(choice == 4){
                                checkAvailableSlotForIndexNum(sc);
                            }
                            if(choice == 5){
                                printStudentListByIndexNum(sc);
                            }
                            if(choice == 6){
                                printStudentListByCourse(sc);
                            }
                            if(choice == 7){
                                quit = true;
                            }
                        }
                    }    
                }
            } else {
                System.out.println("Username/password incorrect...");
            }
        }
    }

    private static boolean checkAccessPeriod(String start, String end){
        SimpleDateFormat frmt = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        try{
            //further format dates
            start = start.replace('/', ' ');
            end = end.replace('/', ' ');
            start = start.split(" ")[1] +" "+ start.split(" ")[2];
            end = end.split(" ")[1] +" "+ end.split(" ")[2];

            Date startDt = frmt.parse(start);
            Date endDt = frmt.parse(end);
    
            long sinceEpochStart = startDt.getTime();
            long sinceEpochEnd = endDt.getTime();
            long now = Instant.now().toEpochMilli();

            if(now < sinceEpochEnd && now > sinceEpochStart){
                return true;
            } else {
                return false;
            }
            
        } catch (ParseException e){
            System.out.println(e.getMessage());
            return false;
        }
        
    }

    private static void swopIndexNumberWithAnother(Scanner sc){
        //assume both students have enrolled in the same course
        printCoursesRegistered();
        System.out.print("Enter course index to be swapped: ");
        int courseIndex= sc.nextInt();
        //find course
        Course c = getCourse(courseIndex);
        //find out who else is taking this course
        //save a mapping of student username and course index
        //use it later when remove a student
        TreeMap<String,Integer> tempMap = new TreeMap<String,Integer>();
        ArrayList<Integer> indexes = c.getIndex();
        for(int k=0; k<indexes.size(); k++){
            int currIndex = indexes.get(k).intValue();
            if(currIndex!= courseIndex){ //for every index not taken
                ArrayList<String> stds = c.getStudentListFor(currIndex);
                ArrayList<Student> studentsTaking = new ArrayList<Student>();
                for(int i=0; i< stds.size(); i++){
                    //get corresponding student
                    Student s = getStudentByMetricNum(stds.get(i));
                    studentsTaking.add(s);
                    System.out.println("Student: "+s.getName()+", Taking Course: "+c.getCourseCode()+", Course Index: "+currIndex);
                    tempMap.put(s.getName(), currIndex);
                }
            }
        }
        System.out.println("Please enter student name you wish to swap with: ");
        String username = sc.next();
        Student newStudent = getStudentByUsername(username);
        System.out.println("Enter user's password: ");
        String password = sc.next();
        String salt = getSalt(username);
        if(!salt.equals("")){
            String passSalt = password+salt;
            String hash = toHash(passSalt.trim());
            String hashFromShadow = getHashFromShadow(username);
            if(hash.trim().equals(hashFromShadow.trim())){
                System.out.println("Swapping course index now...");
                //drop course for new student
                int newStudentCourseIndex = tempMap.get(newStudent.getName()).intValue();
                c.removeStudent(newStudent.getMetricNumber(), newStudentCourseIndex);
                ArrayList<String> lines = convertCoursesIntoLines();
                overWriteWholeFile(lines, "courses.txt");
                System.out.println(username+" successfully removed from index: "+newStudentCourseIndex);
                //drop course for curr student
                removeOldStudent(courseIndex,c);
                //perform the swap
                c.addStudent(newStudent.getMetricNumber(), courseIndex);
                lines = convertCoursesIntoLines();
                overWriteWholeFile(lines, "courses.txt");
                addNewStudent(newStudentCourseIndex,c);
                System.out.println("Students successfully swapped!");
            }
        }else {
            System.out.println("Username/password incorrect...");
        }

    }

    private static void changeIndexNumberOfCourse(Scanner sc){
        printCoursesRegistered();
        System.out.print("Enter course index to be changed: ");
        int courseIndex= sc.nextInt();
        //find course
        Course c = getCourse(courseIndex);
        //display all indexes
        ArrayList<Integer> idx = c.getIndex();
        String indexes = "";
        for(int i=0; i< idx.size(); i++){
            if(idx.get(i) != courseIndex){
                indexes += idx.get(i);
            }
        }
        System.out.println("Which new index do you want? ("+indexes+"): ");
        int newCourseIndex= sc.nextInt();
        //remove old student
        removeOldStudent(courseIndex,c);
        //add student to new course
        addNewStudent(newCourseIndex,c);
        System.out.println("Index successfully changed!");

    }

    private static void checkVacanciesAvailable(Scanner sc){
        displayAllCourses();
        System.out.print("Enter course index to check vacancies: ");
        int courseIndex = sc.nextInt();
        Course crs = getCourse(courseIndex);
        int vacancy = crs.getVacancyFor(courseIndex);
        System.out.println("Number of vacancies for "+crs.getName()+": "+vacancy);
    }

    private static void printCoursesRegistered(){
        System.out.println("Displaying all courses taken by: "+currentStudent.getName());
        System.out.println("============================");
        ArrayList<CourseTaken> coursesTaken = getCoursesTakenByStudent();
        for(int i=0; i<coursesTaken.size(); i++){
            System.out.println("Course name: "+coursesTaken.get(i).getName());
            System.out.println("Course code: "+coursesTaken.get(i).getCourseCode());
            System.out.println("Course index: "+coursesTaken.get(i).getCourseIndex());
            System.out.println("----------------------------");
        }
    }

    //use getCoursesTakenByStudent to find if there are clashes with subjects being take
    private static ArrayList<CourseTaken> getCoursesTakenByStudent(){
        ArrayList<CourseTaken> crs = new ArrayList<CourseTaken>(); //store all course codes taken by student
        //for each course in courses
        for(int i=0; i< courses.size(); i++){
            Course curr = courses.get(i);
            //for each index in course
            for(int j=0; j<curr.getIndex().size(); j++){
                //go thru the list of students till it matches curr student logged in
                ArrayList<String> stds = curr.getStudentListFor(curr.getIndex().get(j));
                for(int k=0; k< stds.size(); k++){
                    if(stds.get(k).equals(currentStudent.getMetricNumber())){ //student has taken this course
                        //save course in special Object as such:
                        CourseTaken c = new CourseTaken(curr.getName(), curr.getCourseCode(), curr.getIndex().get(j), curr.getClassScheduleFor(curr.getIndex().get(j)));
                        crs.add(c);
                    }
                }
            }
        }
        return crs;
    }

    private static void removeOldStudent(int courseIndex, Course crs){
        crs.removeStudent(currentStudent.getMetricNumber(), courseIndex);
        ArrayList<String> lines = convertCoursesIntoLines();
        overWriteWholeFile(lines, "courses.txt");
        System.out.println("Student successfully removed from course!");
    }

    private static void dropStudentFromCourse(Scanner sc){
        printCoursesRegistered();
        System.out.print("Enter course index to be dropped: ");
        int courseIndex = sc.nextInt(); 
        Course crs = getCourse(courseIndex);
        removeOldStudent(courseIndex, crs);
    }

    private static void displayAllCourses() {
        System.out.println("Displaying all courses: ");
        System.out.println("============================");
        for(int i=0; i<courses.size(); i++){
            Course currCourse = courses.get(i);
            String courseName = currCourse.getName();
            String courseCode = currCourse.getCourseCode();
            ArrayList<Integer> idx = currCourse.getIndex();
            System.out.println("Course name: "+courseName);
            System.out.println("Course code: "+courseCode);
            //print out course and all the indexes and all info 
            for(int k=0; k<idx.size(); k++){
                System.out.println("----------------------------");
                System.out.println("   Course index: "+ idx.get(k));
                String venue = currCourse.getVenueFor(idx.get(k));
                System.out.println("   Venue: "+ venue);
                String schedule = currCourse.getClassScheduleFor(idx.get(k));
                System.out.println("   Schedule: "+ formatSchedule(schedule));
                int vacancy = currCourse.getVacancyFor(idx.get(k));
                System.out.println("   Vacancy: "+ vacancy);
            }
            System.out.println("============================");
        }
    }

    private static void addNewStudent(int courseIndex, Course crs){
        if(getVacancy(courseIndex) != 9999){ //if there is a slot available
            if(getVacancy(courseIndex) != 0 ){
                if(!checkIfStudentIsTakingCourse(currentStudent.getMetricNumber(), crs.getCourseCode())){ //check if student is already taking this course
                    //add student into course object
                    crs.addStudent(currentStudent.getMetricNumber(), courseIndex);
                    //convert courses object to array of lines
                    ArrayList<String> lines = convertCoursesIntoLines();
                    //overwrite current course.txt file
                    overWriteWholeFile(lines, "courses.txt");
                    System.out.println("Student successfully enrolled!");
                } else {
                    System.out.println("Cannot enrol into course. Student already taking course");
                }
            } else {
                //TODO: add student into the waiting list
                
                System.out.println("Cannot enrol into course. No available slots");
            }
        } else {
            System.out.println("Course index entered is invalid");
        }
    }

    private static void addStudentToCourse(Scanner sc){
        //show all the courses first -> show code and indexes
        displayAllCourses();
        System.out.print("Enter course index to add: ");
        int courseIndex = sc.nextInt();
        Course crs = getCourse(courseIndex);
        //add new student
        addNewStudent(courseIndex, crs);
    }

    private static ArrayList<String> convertCoursesIntoLines() {
        ArrayList<String> lines = new ArrayList<String>();
        for(int i=0; i<courses.size(); i++){
            lines.add(courses.get(i).toCourseString());
        }
        return lines;
    }
    
    private static Course getCourse(int index){
        for(int i=0; i<courses.size(); i++){
            if(courses.get(i).indexExists(index)){
                return courses.get(i);
            }
        }
        return null;
    }

    private static boolean checkIfStudentIsTakingCourse(String metricNum, String courseCode){
        for(int i = 0; i< courses.size(); i++) {
            if(courses.get(i).getCourseCode().equals(courseCode)){
                Course course = courses.get(i);
                ArrayList<Integer> idx = course.getIndex();
                //for each index
                for(int k=0; k<idx.size(); k++){
                    if(courses.get(i).findStudent(metricNum, idx.get(k))){
                        return true;
                    }
                }
                //check if student is enrolled in this course
            }   
        }
        return false;
    }


    private static String formatSchedule(String schedule){
        String[] s = schedule.split(",");
        return s[0]+", "+s[1]+" - "+s[2];
    }

    private static Student getStudentByUsername(String username){
        for(int i=0; i<students.size(); i++){
            if(students.get(i).getName().equals(username)){
                return students.get(i);
            }
        }
        return null;
    }

    private static boolean studentExists(Student s){
        for(int i=0; i<students.size(); i++){
            if(students.get(i).getName().equals(s.getName())){
                return true;
            }
        }
        return false;
    }

    private static ArrayList<Integer> processIndexes(String indexes){
       ArrayList<Integer> idx = new ArrayList<Integer>();
       for(int i=0; i < indexes.split(",").length; i++){
            idx.add(Integer.parseInt(indexes.split(",")[i]));
       }
       return idx;
    }

    private static void printStudentListByCourse(Scanner sc){
        System.out.print("Enter course code: ");
        String courseCode = sc.next();
        //go thru all courses
        for(int i=0; i<courses.size(); i++){
            if(courses.get(i).getCourseCode().equals(courseCode)){
                System.out.println("Printing all students for course code: "+ courseCode);
                Course course = courses.get(i);
                //get all students in that course
                ArrayList<Integer> indexes = course.getIndex();
                for(int k=0; k< indexes.size(); k++){
                   ArrayList<String> stdMetricNums = course.getStudentListFor(indexes.get(k));
                   for(int x=0; x< stdMetricNums.size(); x++){
                        Student studentFound = getStudentByMetricNum(stdMetricNums.get(x));
                        studentFound.printStudent();
                   }
                }
            }
        }
    }

    private static void printStudentListByIndexNum(Scanner sc){
        System.out.print("Enter the index number of the course: ");
        String indexNum = sc.next();
        ArrayList<String> stds = getStudents(Integer.parseInt(indexNum));
        if(stds != null){
            //print out each student
            System.out.println("Students in index "+ indexNum+ ": ");
            for(int i=0; i< stds.size(); i++){
                Student studentFound = getStudentByMetricNum(stds.get(i));
                studentFound.printStudent();
            }
        } else {
            System.out.println("Course index entered is invalid");
        }
    }

    private static Student getStudentByMetricNum(String metricNum){
        for(int i=0; i<students.size(); i++){
            if(students.get(i).getMetricNumber().equals(metricNum)){
                return students.get(i);
            }
        }
        return null;
    }

    private static void checkAvailableSlotForIndexNum(Scanner sc) {
        //get the course index
        //for each course, find if this course index exists
        //if it does, return the vacancy for that index
        System.out.print("Enter the index number of the course: ");
        String indexNum = sc.next();
        int v = getVacancy(Integer.parseInt(indexNum));
        if(v != 9999){
            System.out.println("Number of available slots for index "+ indexNum+ " is " + v);
        } else {
            System.out.println("Course index entered is invalid");
        }
    }

    private static ArrayList<String> getStudents(int index){ //check if index exists in any of the courses, if it does return that course
        for(int i = 0; i< courses.size(); i++) { //for each course
            for(int k=0; k< courses.get(i).getIndex().size(); k++){ //for each index
                if(courses.get(i).getIndex().get(k).intValue() == index) { //if index matches
                    return courses.get(i).getStudentListFor(index);
                }
            }
        }
        return null;
    }

    private static int getVacancy(int index){ //check if index exists in any of the courses, if it does return that course
        for(int i = 0; i< courses.size(); i++) {
            for(int k=0; k< courses.get(i).getIndex().size(); k++){
                if(courses.get(i).getIndex().get(k).intValue() == index) {
                    return courses.get(i).getVacancyFor(index);
                }
            }
        }
        return 9999;
    }

    private static void addCourse(Scanner sc){
        System.out.print("Enter course name: ");
        String courseName = sc.next();
        System.out.print("Enter course code: ");
        //check if course code already exists
        String courseCode = sc.next();
        System.out.print("Enter school: ");
        String school = sc.next();
        System.out.print("Enter course info: ");
        String courseInfo = sc.next();
        System.out.print("Enter Indexes (e.g. 12,334,234): ");
        String indexes = sc.next();
        ArrayList<Integer> idx = processIndexes(indexes); // return Arraylist of indexes
        TreeMap<Integer, String> classSchedule = new TreeMap<Integer, String>();
        TreeMap<Integer, String> venue = new TreeMap<Integer, String>();
        TreeMap<Integer, Integer> vacancy = new TreeMap<Integer, Integer>(); //vacancy per index
        TreeMap<Integer,ArrayList<String>> students = new TreeMap<Integer, ArrayList<String>>(); 
        for(int i=0; i< idx.size(); i++){
            System.out.print("Enter schedule for index "+idx.get(i)+" eg: MON,1930,2230: ");
            String schedule = sc.next();
            classSchedule.put(idx.get(i), schedule);
            System.out.print("Enter venue for index "+idx.get(i)+": ");
            String venueName = sc.next();
            venue.put(idx.get(i), venueName);
            //hardcode vacancy
            vacancy.put(idx.get(i), 10);
            students.put(idx.get(i), new ArrayList<String>());
        }
        String courseString = Course.toCourseString(courseName, courseCode, school, courseInfo, idx,classSchedule,venue,vacancy);
        Course c = new Course(courseName, courseCode, school, courseInfo, idx,classSchedule,venue,vacancy);
        writeToFile(courseString,"courses.txt");
        courses.add(c);
        printCourses();
    }

    private static void addStudent(Scanner sc){
        System.out.print("Enter student username: ");
        String username = sc.next();
        if(!exist(username)){
            System.out.print("Password: ");
            String password = sc.next();
            System.out.print("Confirm Password: ");
            String confirmPassword = sc.next();
            if(confirmPassword.equals(password)){
                if(isPasswordStrong(password)){
                    String salt = generateSalt();
                    //get student details
                    System.out.print("Enter metric number: ");
                    String metric = sc.next();
                    System.out.print("Enter gender: ");
                    String gender = sc.next();
                    System.out.print("Enter nationality: ");
                    String nationality = sc.next();
                    //check if student already exists
                    //save student details to local arraylist
                    Student s = new Student(username, metric, gender, nationality);
                    if(!studentExists(s)){
                        students.add(s);
                        //save student details to file
                        writeToFile(username+":"+metric+":"+gender+":"+nationality,"students.txt");
                        writeToFile(username+":"+salt,"salt.txt");
                        String passSalt = password+salt;
                        String hash = toHash(passSalt);
                        writeToFile(username+":"+hash+":"+"S","shadow.txt");
                        System.out.println("Student added!");
                        printStudents();
                    } else {
                        System.out.println("Student with similar username already exists...");
                    }
                } else {
                    System.out.println("Password not strong enough...");
                    System.out.println("Make sure password is greater than 8 characters long");
                }
            }  else {
                System.out.println("Passwords mismatch...");
            }
        } else {
            System.out.println("User already exists!");
        }
    }

    private static void editStudentAccessPeriod(Scanner sc) {
        //check if access period exists in file
        int startTimeIndex = 9999;
        String stFound = "";
        int endTimeIndex = 9999;
        String etFound = "";
        for(int i = 0; i < settings.size(); i++){
            if(settings.get(i).split(" ")[0].equals("startTime")){
                //extract out start time
                startTimeIndex = i;
                stFound = settings.get(i).split(" ")[1];
            }
        }
        for(int i = 0; i < settings.size(); i++){
            if(settings.get(i).split(" ")[0].equals("endTime")){
                //extract out start time
                endTimeIndex = i;
                etFound = settings.get(i).split(" ")[1];
            }
        }
        if(startTimeIndex == 9999 && endTimeIndex == 9999) {
            //if no start/end time
            System.out.println("No student access period found...");
            //DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy/HH:mm:ss");
            System.out.println("Enter start time for access period: (dd-MM-yyyy/HH:mm:ss)");
            String startTime = sc.next();
            //LocalDateTime dt = LocalDateTime.parse(startTime, myFormatObj);
            System.out.println("Enter end time for access period: (dd-MM-yyyy/HH:mm:ss)");
            String endTime = sc.next();
            //save datetime in txt file
            settings.add("startTime "+startTime);
            settings.add("endTime "+endTime);
            overWriteWholeFile(settings, "settings.txt");
            System.out.println("Student access period saved!");
        }else {
            System.out.println("Current student start and end time:");
            System.out.println("Start Time: "+ stFound);
            System.out.println("End Time: "+ etFound);
            System.out.println("Would you like to modify? Y/n:");
            String accessTimeModification = sc.next();
            if(accessTimeModification.equals("Y")){
                //get new timings
                System.out.println("Enter start time for access period: (dd-MM-yyyy/HH:mm:ss)");
                String startTime = sc.next();
                //LocalDateTime dt = LocalDateTime.parse(startTime, myFormatObj);
                System.out.println("Enter end time for access period: (dd-MM-yyyy/HH:mm:ss)");
                String endTime = sc.next();
                //remove previous dates
                findAndRemoveSettings("startTime");
                findAndRemoveSettings("endTime");
                //save datetime in txt file
                settings.add("startTime "+startTime);
                settings.add("endTime "+endTime);
                overWriteWholeFile(settings, "settings.txt");
                System.out.println("Student access period modified!");
            }
        }
    }

    private static void findAndRemoveSettings(String searchTerm){
        //String searchTerm = line.split(" ")[0];
        for(int i=0; i< settings.size(); i++){
            String curr = settings.get(i).split(" ")[0];
            if(curr.equals(searchTerm)){
                settings.remove(i);
            }
        }
    }

    private static void printCourses(){
        for(int i=0; i<courses.size(); i++){
            System.out.println("Course: "+courses.get(i).getName() +", Course Code: "+courses.get(i).getCourseCode());
        }
    }

    private static void printStudents(){
        for(int i=0; i<students.size(); i++){
            System.out.println("Student: "+students.get(i).getName() +", Metric Number: "+students.get(i).getMetricNumber());
        }
    }

    private static void loadCoursesFromFile() {
        courses.clear();
        try {
            FileReader reader = new FileReader("courses.txt");
            int character;
            String line = "";
            while ((character = reader.read()) != -1) {
                if((char) character == '\n'){
                    //line of student information
                    courses.add(Course.fromStringToCourse(line));
                    line = "";
                }else {
                    line += (char) character;
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadStudentsFromFile() {
        students.clear();
        try {
            FileReader reader = new FileReader("students.txt");
            int character;
            String line = "";
            while ((character = reader.read()) != -1) {
                if((char) character == '\n'){
                    //line of student information
                    students.add(Student.fromStringToStudent(line));
                    line = "";
                }else {
                    line += (char) character;
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadSettingsFromFile() {
        settings.clear();
        try {
            FileReader reader = new FileReader("settings.txt");
            int character;
            String line = "";
            while ((character = reader.read()) != -1) {
                if((char) character == '\n'){
                    settings.add(line);
                    line = "";
                }else {
                    line += (char) character;
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void writeToFile(String line, String fileName){
        try {
            FileWriter writer = new FileWriter(fileName, true);
            writer.write(line);
            writer.write("\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void overWriteWholeFile(ArrayList<String> lines, String fileName){
        try {
            FileWriter writer = new FileWriter(fileName, false);
            for(int i=0; i< lines.size(); i++){
                writer.write(lines.get(i));
                writer.write("\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isPasswordStrong(String password){
        if(password.length() < 9){
            return false;
        }
        return true;
    }

    private static String extractUsername(String line){
        return line.split(":")[0];
    }
    private static String extractSalt(String line){
        return line.split(":")[1];
    }
    private static String extractHash(String line){
        return line.split(":")[1];
    }
    private static String extractUserType(String line){
        return line.split(":")[2];
    }
    private static String getUserTypeFromShadow(String username){
        try {
            FileReader reader = new FileReader("shadow.txt");
            int character;
            String line = "";
            while ((character = reader.read()) != -1) {
                if((char) character == '\n'){
                    if(username.equals(extractUsername(line))){
                        return extractUserType(line);
                    }
                    line = "";
                }else {
                    line += (char) character;
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return "";
    }
    private static String getHashFromShadow(String username){
        try {
            FileReader reader = new FileReader("shadow.txt");
            int character;
            String line = "";
            while ((character = reader.read()) != -1) {
                if((char) character == '\n'){
                    if(username.equals(extractUsername(line))){
                        return extractHash(line);
                    }
                    line = "";
                }else {
                    line += (char) character;
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return "";
    }
    private static String getSalt(String username){
        try {
            FileReader reader = new FileReader("salt.txt");
            int character;
            String line = "";
            while ((character = reader.read()) != -1) {
                if((char) character == '\n'){
                    if(username.equals(extractUsername(line))){
                        return extractSalt(line);
                    }
                    line = "";
                }else {
                    line += (char) character;
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return "";
    }

    private static boolean exist(String username){
        try {
            FileReader reader = new FileReader("salt.txt");
            int character;
            String line = "";
            while ((character = reader.read()) != -1) {
                line += (char) character;
                if((char) character == '\n'){
                    if(username.equals(extractUsername(line))){
                        return true;
                    }
                    line = "";
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    private static String generateSalt(){
        String salt = "";
        Random r = new Random();
        for(int i=0; i<8; i++){
            String randomDigit = Integer.toString(r.nextInt(10));
            salt += randomDigit;
        }
        return salt;
    }

    private static String toHash(String passwordSalt){

        try { 
            MessageDigest md = MessageDigest.getInstance("MD5"); 
            byte[] messageDigest = md.digest(passwordSalt.getBytes()); 
            BigInteger no = new BigInteger(1, messageDigest); 
            String hashtext = no.toString(16); 
            while (hashtext.length() < 32) { 
                hashtext = "0" + hashtext; 
            } 
            return hashtext; 
        }  
        catch (NoSuchAlgorithmException e) { 
            throw new RuntimeException(e); 
        }  
    }
}