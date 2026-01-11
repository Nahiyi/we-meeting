package cn.clazs.easymeeting.entity.query;

import lombok.Data;

@Data
public class UserQuery {

    private Integer pageNo = 1;
    private Integer pageSize = 10;
    private String nickName;
    private Integer status;

    public Integer getOffset() {
        return (pageNo - 1) * pageSize;
    }
}