package com.uberTim12.ihor.repository.communication;

import com.uberTim12.ihor.model.communication.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface IMessageRepository extends JpaRepository<Message, Integer>{
    @Query("select m from Message m where m.ride is not null and m.ride.id = ?2 and (m.receiver.id = ?1 or m.sender.id = ?1)")
    public List<Message> findAllByRideIdAndSenderIdOrReceiverId(Integer user_id, Integer ride_id);
    @Query("select m from Message m where m.ride is not null and (m.receiver.id = ?1 or m.sender.id = ?1)")
    public List<Message> findAllBySenderIdOrReceiverId(Integer user_id);

    @Query("select m from Message m where m.receiver is null or m.sender is null order by m.sendTime")
    public List<Message> findAllForAdmin();
    @Query("select m from Message m where (m.receiver is null and m.sender.id=?1) or (m.sender is null and m.receiver.id=?1) order by m.sendTime")
    public List<Message> findAllWithAdmin(Integer userId);
}
