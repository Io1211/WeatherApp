package at.qe.skeleton.internal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

import java.util.Date;

@Entity
public class Subscription {


    @Id
    private Long id;

    @OneToOne
    private Userx userx;

    @Column(name = "startDate",nullable = false)
    private Date startDate;
    @Column(name = "duration")
    private int duration;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
