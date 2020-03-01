package com.idugalic.commandside.blog.web;

import com.idugalic.commandside.blog.command.CreateBlogPostCommand;
import com.idugalic.commandside.blog.command.PublishBlogPostCommand;
import com.idugalic.common.model.AuditEntry;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.security.Principal;

/**
 * A web controller for managing {@link BlogPostAggregate} - create/update only.
 *
 * @author idugalic
 */
@RestController
@RequestMapping(value = "/blogpostcommands")
public class BlogController {

    private static final Logger LOG = LoggerFactory.getLogger(BlogController.class);

    private String getCurrentUser() {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        }
        return null;
    }

    private AuditEntry createAudit() {
        return new AuditEntry(getCurrentUser());
    }

    @Autowired
    private CommandGateway commandGateway;

    /**
     * FIXME 复杂系统，比较好，因为因为所有的请求，全部使用了中间件作为缓存；业务小根本没有这个压力啊；何必搞得这么复杂；增加了mq的复杂度和风险；
     * FIXME 系统之间使用该方式比较好，自己实现了，岂不是容易了；何必搞得这么复杂；
     * FIXME 结论：1.适合复杂系统，对系统吞吐量要求比较搞得功能；
     *            2.小系统不适合；成本高，又有延迟；
     */
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.CREATED)
    public void create(@RequestBody CreateBlogPostRequest request, HttpServletResponse response, Principal principal) {
        LOG.debug(CreateBlogPostRequest.class.getSimpleName() + " request received");

        CreateBlogPostCommand command = new CreateBlogPostCommand(createAudit(), request.getTitle(),
                request.getRawContent(), request.getPublicSlug(), request.getDraft(), request.getBroadcast(),
                request.getPublishAt(), request.getCategory(), getCurrentUser());
        commandGateway.sendAndWait(command);

        //FIXME 发布到mq，是否会延迟？？？？？？？？中间件增加的mq的装换，
        //FIXME 是否有延迟？？？？？？？如何解决该延迟的；


        LOG.debug(CreateBlogPostCommand.class.getSimpleName() + " sent to command gateway: Blog Post [{}] ", command.getId());
    }

    //FIXME 根是名词复数，其他uri是动词名词；
    //FIXME 修改返回结果挺好，很适合该实现的方式；因为发布，并不一定成功；有可能缓存到消息中间件；

    @RequestMapping(value = "/{id}/publishcommand", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    public void publish(@PathVariable String id, @RequestBody PublishBlogPostRequest request, HttpServletResponse response, Principal principal) {
        LOG.debug(PublishBlogPostRequest.class.getSimpleName() + " request received");

        PublishBlogPostCommand command = new PublishBlogPostCommand(id, createAudit(), request.getPublishAt());
        commandGateway.sendAndWait(command);
        LOG.debug(PublishBlogPostCommand.class.getSimpleName() + " sent to command gateway: Blog Post [{}] ", command.getId());
    }

}
