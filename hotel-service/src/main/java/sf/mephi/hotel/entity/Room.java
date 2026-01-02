package sf.mephi.hotel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sf.mephi.common.constants.RoomType;

import java.math.BigDecimal;

@Entity
@Table(
        name = "rooms",
        uniqueConstraints = @UniqueConstraint(columnNames = {"hotel_id", "room_number"})
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @Column(name = "room_number", nullable = false, length = 20)
    private String roomNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 50)
    private RoomType roomType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    @Builder.Default
    private Boolean available = true;

    @Column(name = "times_booked", nullable = false)
    @Builder.Default
    private Integer timesBooked = 0;

    @Version
    private Integer version;

    //Временная блокировка для саги
    @Transient
    private String currentRequestId;

    public void incrementTimesBooked() {
        this.timesBooked++;
    }

    public void decrementTimesBooked() {
        if (this.timesBooked > 0) {
            this.timesBooked--;
        }
    }
}
