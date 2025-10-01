package com.tikkeul.mote.repository;

import com.tikkeul.mote.entity.Notice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    List<Notice> findByTitleContainingOrContentContaining(String titleKeyword, String contentKeyword, Sort sort);
}
