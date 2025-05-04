package com.example.demo.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FindCompareDto {

    private String valIncorrect;

    private String valCorrect;

    private long numericalOrder;
}
