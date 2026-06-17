package com.lp.criticabackend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "chart_item")
public class ChartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snapshot_id", nullable = false)
    private ChartSnapshot snapshot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    private Integer position;

    @Column(name = "position_gain")
    private Integer positionGain;

    @Column(name = "days_charting")
    private Integer daysCharting;

    @Column(name = "day_streams")
    private Long dayStreams;

    @Column(name = "day_streams_gain")
    private Long dayStreamsGain;

    @Column(name = "week_streams")
    private Long weekStreams;

    @Column(name = "week_streams_gain")
    private Long weekStreamsGain;

    @Column(name = "total_streams")
    private Long totalStreams;

    // --- Constructors ---

    public ChartItem() {}

    public ChartItem(Integer position, Integer positionGain, Song song,
                     Integer daysCharting,
                     Long dayStreams, Long dayStreamsGain,
                     Long weekStreams, Long weekStreamsGain,
                     Long totalStreams) {
        this.position = position;
        this.positionGain = positionGain;
        this.song = song;
        this.daysCharting = daysCharting;
        this.dayStreams = dayStreams;
        this.dayStreamsGain = dayStreamsGain;
        this.weekStreams = weekStreams;
        this.weekStreamsGain = weekStreamsGain;
        this.totalStreams = totalStreams;
    }

    // --- Getters & Setters ---

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public ChartSnapshot getSnapshot() { return snapshot; }
    public void setSnapshot(ChartSnapshot snapshot) { this.snapshot = snapshot; }

    public Song getSong() { return song; }
    public void setSong(Song song) { this.song = song; }

    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }

    public Integer getPositionGain() { return positionGain; }
    public void setPositionGain(Integer positionGain) { this.positionGain = positionGain; }

    public Integer getDaysCharting() { return daysCharting; }
    public void setDaysCharting(Integer daysCharting) { this.daysCharting = daysCharting; }

    public Long getDayStreams() { return dayStreams; }
    public void setDayStreams(Long dayStreams) { this.dayStreams = dayStreams; }

    public Long getDayStreamsGain() { return dayStreamsGain; }
    public void setDayStreamsGain(Long dayStreamsGain) { this.dayStreamsGain = dayStreamsGain; }

    public Long getWeekStreams() { return weekStreams; }
    public void setWeekStreams(Long weekStreams) { this.weekStreams = weekStreams; }

    public Long getWeekStreamsGain() { return weekStreamsGain; }
    public void setWeekStreamsGain(Long weekStreamsGain) { this.weekStreamsGain = weekStreamsGain; }

    public Long getTotalStreams() { return totalStreams; }
    public void setTotalStreams(Long totalStreams) { this.totalStreams = totalStreams; }
}