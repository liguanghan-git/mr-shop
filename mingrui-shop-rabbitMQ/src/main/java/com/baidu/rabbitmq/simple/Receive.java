package com.baidu.rabbitmq.simple;

import com.baidu.rabbitmq.utils.RabbitmqConnectionUtil;
import com.rabbitmq.client.*;

import java.io.IOException;

/**
 * @ClassName TestEurekaFeignController
 * @Description: TODO
 * @Author liguanghan
 * @Date 2020/10/9
 * @Version V1.0
 **/

public class Receive {

    //队列名称
    private final static String QUEUE_NAME = "simple_queue";

    public static void main(String[] arg) throws Exception {
        // 获取连接
        Connection connection = RabbitmqConnectionUtil.getConnection();
        // 创建通道
        Channel channel = connection.createChannel();
        // 声明队列
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        // 定义队列 接收端==》消费者   Consumer-->肯搜莫
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            // 监听队列中的消息，如果有消息，进行处理
            // @Override修饰抽象方法
            //第一个 consumerTag唯一的
            //第二个 envelope交换
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                // body： 消息中参数信息
                String msg = new String(body);
                System.out.println(" 收到消息，执行中 : " + msg + "!");
                //System.out.println(1 /0 );

            }
        };
       /*
       param1 : 队列名称

       param2 : 是否自动确认消息
       param3 : 消费者
        */
        //自动确认提交
        channel.basicConsume(QUEUE_NAME, true, consumer);

        //消费者需要时时监听消息，不用关闭通道与连接
    }

}
