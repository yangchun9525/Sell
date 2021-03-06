package com.example.sell.controller;

import com.example.sell.converter.OrderForm2OrderDTOConverter;
import com.example.sell.dto.OrderDTO;
import com.example.sell.enums.ResultEnum;
import com.example.sell.exception.ResponseBankException;
import com.example.sell.exception.SellException;
import com.example.sell.form.OrderForm;
import com.example.sell.service.BuyerService;
import com.example.sell.service.OrderService;
import com.example.sell.utils.ResultVOUtil;
import com.example.sell.vo.ResultVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 买家订单相关
 */
@RestController
@RequestMapping("/buyer/order")
public class BuyerOrderController {
    private final Logger logger = LoggerFactory.getLogger(BuyerOrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private BuyerService buyerService;


    //创建订单
    @PostMapping("/create")
    public ResultVO<Map<String, String>> create(@Valid OrderForm orderForm, BindingResult bindingResult) {
        logger.info("yc-orderForm:" + orderForm.toString());
        if (bindingResult.hasErrors()) {
            logger.error("【创建订单】 参数不正确 ,orderForm{}", orderForm);
            throw new SellException(ResultEnum.PARAM_ERROR.getCode(), bindingResult.getFieldError().getDefaultMessage());
        }
        OrderDTO orderDTO = OrderForm2OrderDTOConverter.convert(orderForm);
        if (CollectionUtils.isEmpty(orderDTO.getOrderDetailList())) {
            logger.error("【创建订单】 购物车不能为空");
            throw new SellException(ResultEnum.CART_EMPTY);
        }
        OrderDTO createResult = orderService.create(orderDTO);
        logger.info("yc-i" + createResult.toString());
        Map<String, String> map = new HashMap<>();
        map.put("orderId", createResult.getOrderId());
//       重定向 return new ModelAndView("redirect:/buyer/product/list");
        return ResultVOUtil.success(map);
    }

    //订单列表
    @GetMapping("/list")
    public ResultVO<List<OrderDTO>> list(
            @RequestParam("openid") String openid,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        if (StringUtils.isEmpty(openid)) {
            logger.error("【查询订单列表】 openid为空");
            throw new SellException(ResultEnum.PARAM_ERROR);
//            throw new ResponseBankException();
        }
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        PageRequest request = PageRequest.of(page, size, sort);
        Page<OrderDTO> orderDTOPage = orderService.findList(openid, request);

        return ResultVOUtil.success(orderDTOPage.getTotalElements() + "", orderDTOPage.getContent());
    }

    //订单详情
    @GetMapping("/detail")
    public ResultVO<OrderDTO> detail(@RequestParam("openid") String openid,
                                     @RequestParam("orderId") String orderId) {
        OrderDTO orderDTO = buyerService.findOrderOne(openid, orderId);
        return ResultVOUtil.success(orderDTO);
    }

    //取消订单
    @PostMapping("/cancel")
    //@RequestBody 加入这个那么参数就是一个map（@RequestBody Map map），解析时就从map中获取数据进行解析,参数就是一个json对象
    public ResultVO cancel(@RequestParam("openid") String openid,
                           @RequestParam("orderId") String orderId) {
        logger.info(openid);
        logger.info(orderId);
        buyerService.cancelOrder(openid, orderId);
        return ResultVOUtil.success();
    }
}
