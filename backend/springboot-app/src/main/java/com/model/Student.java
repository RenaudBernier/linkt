import java.util.ArrayList;

public class Student extends User
{

    private Long studentId;
    private ArrayList<SavedEvent> savedEvents = new ArrayList<>();
    private ArrayList<Ticket> tickers = new ArrayList<>();

    public Student() { super();}
    public Student(String email, String firstName, String lastName, String phoneNumber, String password) 
    {
        super(email,firstName,lastName,phoneNumber,password);
    
    }
    public Long getStudentId() {
        return studentId;
    }
    
    public List<SavedEvent> getSavedEvents() {
        return savedEvents;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }
    
    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public void setSavedEvents(List<SavedEvent> savedEvents) {
        this.savedEvents = savedEvents;
    }
    
    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

}
