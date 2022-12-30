package ca.unb.cs2043.project;

import java.time.LocalDate;

public class ToDo {

    public String title;

    public LocalDate due_date;

    public boolean completed;

    public ToDo(String title, LocalDate due_date, boolean completed) {
        this.title = title;
        this.due_date = due_date;
        this.completed = completed;
    }

    public LocalDate getDue_date() {
        return due_date;
    }
}
