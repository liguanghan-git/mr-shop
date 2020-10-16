package com.baidu.test;

import com.baidu.shop.dto.UserInfo;
import com.baidu.shop.utils.JwtUtils;
import com.baidu.shop.utils.RsaUtils;
import org.junit.*;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @ClassName TestEurekaFeignController
 * @Description: TODO
 * @Author liguanghan
 * @Date 2020/10/16
 * @Version V1.0
 **/

public class JwtTokenTest {

    //公钥位置
    private static final String pubKeyPath = "E:\\luosimaojar\\secret\\rea.pub";
    //私钥位置
    private static final String priKeyPath = "E:\\luosimaojar\\secret\\rea.pri";
    //公钥对象
    private PublicKey publicKey;
    //私钥对象
    private PrivateKey privateKey;


    /**
     * 生成公钥私钥 根据密文
     * @throws Exception
     */
    @Test
    public void genRsaKey() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "mingrui");
    }


    /**
     * 从文件中读取公钥私钥
     * @throws Exception
     * @Before 在执行@Test 之前执行
     */
    @Before
    public void getKeyByRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    /**
     * 根据用户信息结合私钥生成token  载荷？？？
     * @throws Exception
     */
    @Test
    public void genToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(1, "zhaojunhao"), privateKey, 2);
        System.out.println("user-token = " + token);
    }


    /**
     * 结合公钥解析token
     * @throws Exception
     */
    @Test
    public void parseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MSwidXNlcm5hbWUiOiJ6aGFvanVuaGFvIiwiZXhwIjoxNjAyODQ3NTE5fQ.BRZlMty9keCm1m8vrtK3TmejEXQRXOC89YTdS8MjM8HlDe2UE3D3D6m_jsAtfAbXG4caOno_du-bvB8mer53gSVKbz7AxxmcCFD3nbpFyW-OgpyGysQAEwA8PUU-Dfn7sbEjSEgi9Ye4K169saFMg3sx5ZwgmnldlCUPODAYhdg";

        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}
