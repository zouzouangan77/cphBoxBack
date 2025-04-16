package com.psu.rouen.cphbox.web.rest.vm;

import com.psu.rouen.cphbox.domain.ApplicationLog;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ApplicationLogVM {

    private String login;
    private String operation;
    private String endPoint;
    private String method;
    private String params;
    private Instant createDate;

    public ApplicationLogVM(ApplicationLog applicationLog) {
        this.login = applicationLog.getUser() != null ? applicationLog.getUser().getLogin() : "anonymousUser";
        this.operation = applicationLog.getOperation();
        this.endPoint = applicationLog.getEndPoint();
        this.method = applicationLog.getMethod();
        this.params = applicationLog.getParams();
        this.createDate = applicationLog.getCreatedDate();
    }


}
