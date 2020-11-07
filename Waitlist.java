import java.util.ArrayList;
import models.Student;

public class Waitlist {
    private ArrayList<Student> waitlist = new ArrayList<Student>();

    public void addToWaitList(Student s){
        this.waitlist.add(s);
    }

}