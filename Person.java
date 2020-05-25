package finalproject.entities;

public class Person implements java.io.Serializable {
    private String first_name;
    private String last_name;
    private String age;
    private String city;
    private String ID;


    private static final long serialVersionUID = 4190276780070819093L;

    public Person() {
    }

    public Person(String first_name, String last_name, String age, String city, String ID) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.age = age;
        this.city = city;
        this.ID = ID;
    }

    public String getFirst_name() {
        return first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public String getAge() {
        return age;
    }

    public String getCity() {
        return city;
    }

    public String getID() {
        return ID;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    @Override
    public String toString() {
        return "Person{" +
                "first_name='" + first_name + '\'' +
                ", last_name='" + last_name + '\'' +
                ", age='" + age + '\'' +
                ", city='" + city + '\'' +
                ", ID='" + ID + '\'' +
                '}';
    }

}
