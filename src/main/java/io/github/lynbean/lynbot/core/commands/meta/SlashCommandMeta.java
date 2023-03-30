package io.github.lynbean.lynbot.core.commands.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SlashCommandMeta {
    @Nonnull String name();
    @Nonnull String description();

    Option[] options() default {};

    @Retention(RetentionPolicy.RUNTIME)
    @Target({})
    @interface Option {
        @Nonnull OptionType type();
        @Nonnull String name();
        @Nonnull String description();
        boolean required() default false;
        int minIntValues() default Integer.MIN_VALUE;
        int maxIntValues() default Integer.MAX_VALUE;
        double minDoubleValues() default -9007199254740991D;
        double maxDoubleValues() default 9007199254740991D;
        @Nonnull Choice[] choices() default {};
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({})
    @interface Choice {
        @Nonnull String description();
        @Nonnull String value();
    }
}
