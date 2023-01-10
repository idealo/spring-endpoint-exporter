package de.idealo.security.endpointexporter.export

import io.swagger.v3.core.jackson.ApiResponsesSerializer
import io.swagger.v3.core.jackson.CallbackSerializer
import io.swagger.v3.core.jackson.mixin.OperationMixin
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.responses.ApiResponse
import org.springframework.aot.hint.MemberCategory
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar

class ExportRuntimeHintsRegistrar : RuntimeHintsRegistrar {

    override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {

        val jacksonMemberCategories = arrayOf(MemberCategory.DECLARED_FIELDS, MemberCategory.INTROSPECT_PUBLIC_METHODS, MemberCategory.INVOKE_PUBLIC_METHODS);
        hints.reflection()
            .registerType(OpenAPI::class.java, *jacksonMemberCategories)
            .registerType(Info::class.java, *jacksonMemberCategories)
            .registerType(PathItem::class.java, *jacksonMemberCategories)
            .registerType(Operation::class.java, *jacksonMemberCategories)
            .registerType(OperationMixin::class.java, *jacksonMemberCategories)
            .registerType(CallbackSerializer::class.java, MemberCategory.INTROSPECT_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            .registerType(ApiResponsesSerializer::class.java, MemberCategory.INTROSPECT_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            .registerType(Parameter::class.java, *jacksonMemberCategories)
            .registerType(Schema::class.java, *jacksonMemberCategories)
            .registerType(ApiResponse::class.java, *jacksonMemberCategories)
    }
}
