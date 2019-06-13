package com.tuandai.transaction.mapper;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tuandai.transaction.bo.TransactionCheck;

public interface TransactionCheckMapper extends JpaRepository<TransactionCheck, Long> {


    @Modifying
    @Query("delete from transaction_check where msg_id=:msgId")
    void deleteTransactionCheck(@Param("msgId") String msgId);

}
