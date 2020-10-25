package io.bartendr.barback.organization.form

class AddCategoryForm(
        val name: String,
        val penalty: Int,
        val requiredForAll: Boolean,
        val requiredRoleIds: List<Long>
)