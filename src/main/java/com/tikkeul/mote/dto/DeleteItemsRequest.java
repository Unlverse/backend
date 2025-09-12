package com.tikkeul.mote.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class DeleteItemsRequest {
    private List<Long> ids;
}
