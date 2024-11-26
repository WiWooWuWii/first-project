
import java.util.ArrayList;

public class Task extends TM{

    protected String number;
    protected String status;
    protected String title;
    protected String description;
    protected String priority;
    protected String dueDate;
    protected ArrayList<Subtask> subtasks = new ArrayList<>();

    public Task(String number, String status, String title, String description, String priority, String dueDate) {
        this.number = number;
        this.status = status;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
    }
}
