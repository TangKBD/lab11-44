package com.sop.chapter7.productservice.command;

import com.example.core.command.ReserveProductCommand;
import com.example.core.event.ProductReservedEvent;
import com.sop.chapter7.productservice.core.event.ProductCreateEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;

@Aggregate
public class ProductAggregate {

   @AggregateIdentifier
   private String product;
   private String title;
   private BigDecimal price;
   private Integer quantity;

   public ProductAggregate(){

   }

    @CommandHandler
    public ProductAggregate(CreateProductCommand createProductCommand){
        if(createProductCommand.getPrice().compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Price cannot be less than or equal to zero");
        }

        if(createProductCommand.getTitle() == null || createProductCommand.getTitle().isBlank()){
            throw  new IllegalArgumentException("Title cannot be empty");
        }

        ProductCreateEvent productCreateEvent = new ProductCreateEvent();
        BeanUtils.copyProperties(createProductCommand, productCreateEvent);
        AggregateLifecycle.apply(productCreateEvent);

    }

    @CommandHandler
    public void handler(ReserveProductCommand reserveProductCommand){
       if(quantity < reserveProductCommand.getQuantity()) {
           throw new IllegalArgumentException("Insufficient umber  of items in stock");
       }
        ProductReservedEvent productReservedEvent = ProductReservedEvent.builder()
                .orderId(reserveProductCommand.getOrderId())
                .productId(reserveProductCommand.getProductId())
                .quantity(reserveProductCommand.getQuantity())
                .userId(reserveProductCommand.getUserId())
                .build();
       AggregateLifecycle.apply(productReservedEvent);
    }

    @EventSourcingHandler
    public void on(ProductCreateEvent productCreateEvent){
       this.product = productCreateEvent.getProductId();
       this.title = productCreateEvent.getTitle();
       this.price = productCreateEvent.getPrice();
       this.quantity = productCreateEvent.getQuantity();

    }
}
