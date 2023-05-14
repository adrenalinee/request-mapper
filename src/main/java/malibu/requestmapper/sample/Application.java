package malibu.requestmapper.sample;

import malibu.requestmapper.OptionalRequestDispatcher;
import malibu.requestmapper.annotation.RequestMapping;
import malibu.requestmapper.implement.ByPassRequestMapperConfiguration;
import lombok.Data;
import lombok.experimental.Accessors;

public class Application {

    @RequestMapping
    public void handle(Input input) {
        System.out.println("안녕하세요! " + input.type());
    }


    public static void main(String[] args) {
        final OptionalRequestDispatcher<Input, Output> dispatcher = new OptionalRequestDispatcher();
        dispatcher.addRequestMapperConfigurer(
            new ByPassRequestMapperConfiguration<>(Input.class, Output.class)
        );
        dispatcher.registerHandlerObject(new Application());
        dispatcher.bootstrap();

        dispatcher.handle(new Input().type("TEST"))
                .ifPresent(output -> System.out.println(output));
    }
}


@Data
@Accessors(fluent = true)
class Input {
    private String type;
}

@Data
@Accessors(fluent = true)
class Output {

    private String type;

    private String message;
}