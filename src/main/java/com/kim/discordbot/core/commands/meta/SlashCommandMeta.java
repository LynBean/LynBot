package com.kim.discordbot.core.commands.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SlashCommandMeta {
    @Nonnull String name();
    @Nonnull String description() default "";

    Option[] options() default {};

    @Retention(RetentionPolicy.RUNTIME)
    @Target({})
    @interface Option {
        @Nonnull OptionType type();
        @Nonnull String name();
        @Nonnull String description();
        boolean required() default false;
        @Nonnegative int minValues() default 1;
        @Nonnegative int maxValues() default 1;
        @Nonnull Choice[] choices() default {};
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({})
    @interface Choice {
        @Nonnull String description();
        @Nonnull String value();
    }
}
