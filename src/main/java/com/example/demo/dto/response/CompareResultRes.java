package com.example.demo.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompareResultRes {

    private String row;

    private String column;

    private String valSource;

    private String valCompare;
}
