package io.bartendr.barback.organization.form

class AddCategoryForm(
        val name: String,
        val penalty: Int,
        val requiredRoleIds: List<Long>
)