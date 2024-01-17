package com.website.model;

import lombok.Data;
import org.springframework.web.servlet.ModelAndView;

/**
 * 异常返回
 */
@Data
public class RestAndView {

    private ModelAndView modelAndView;

    private Object restData;
}
