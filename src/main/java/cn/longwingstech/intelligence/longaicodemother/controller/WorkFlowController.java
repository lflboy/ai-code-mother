package cn.longwingstech.intelligence.longaicodemother.controller;

import cn.longwingstech.intelligence.longaicodemother.langgraph4j.CodeGenWorkflow;
import org.bsc.langgraph4j.GraphStateException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/workflow")
public class WorkFlowController {

//    @GetMapping(value = "/execute-sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public SseEmitter executeSse(@RequestParam("prompt") String prompt) throws GraphStateException {
//
//    }

    @GetMapping(value = "/execute-flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> executeFlux(@RequestParam("prompt") String prompt) throws GraphStateException {
        CodeGenWorkflow codeGenWorkflow = new CodeGenWorkflow();
        return codeGenWorkflow.executeWorkflowWithFlux( prompt);
    }
}
