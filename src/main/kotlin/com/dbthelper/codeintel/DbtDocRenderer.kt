package com.dbthelper.codeintel

import com.dbthelper.core.DbtUtils
import com.dbthelper.core.model.*

object DbtDocRenderer {

    fun buildNodeDoc(node: DbtNode, index: ManifestIndex): String = buildString {
        append("<html><body style='margin:4px'>")
        append("<h3 style='margin:0 0 6px 0'>${esc(node.name)}</h3>")

        append("<p>")
        append("<code>${node.resourceType}</code>")
        val mat = node.config["materialized"] as? String
        if (mat != null) append(" &middot; <code>$mat</code>")
        append(" &middot; <i>${esc(node.packageName)}</i>")
        append("</p>")

        if (node.database != null || node.schema != null) {
            val parts = listOfNotNull(node.database, node.schema, node.alias ?: node.name)
            append("<p><code>${parts.joinToString(".") { esc(it) }}</code></p>")
        }

        if (node.description.isNotEmpty()) {
            append("<p style='margin:6px 0'>${esc(node.description)}</p>")
        }

        val upstream = index.getUpstream(node.uniqueId)
        val downstream = index.getDownstream(node.uniqueId)
        if (upstream.isNotEmpty() || downstream.isNotEmpty()) {
            append("<hr>")
            if (upstream.isNotEmpty()) {
                val names = upstream.mapNotNull { id -> friendlyName(id, index) }.take(8)
                append("<p><b>Depends on (${upstream.size}):</b> ${names.joinToString(", ") { "<code>${esc(it)}</code>" }}")
                if (upstream.size > 8) append(" +${upstream.size - 8} more")
                append("</p>")
            }
            if (downstream.isNotEmpty()) {
                val names = downstream.mapNotNull { id -> friendlyName(id, index) }.take(8)
                append("<p><b>Used by (${downstream.size}):</b> ${names.joinToString(", ") { "<code>${esc(it)}</code>" }}")
                if (downstream.size > 8) append(" +${downstream.size - 8} more")
                append("</p>")
            }
        }

        if (node.columns.isNotEmpty()) {
            append("<hr><p><b>Columns (${node.columns.size}):</b></p>")
            append("<table style='margin:2px 0'>")
            for ((_, col) in node.columns.entries.take(15)) {
                append("<tr>")
                append("<td><code>${esc(col.name)}</code></td>")
                append("<td style='padding-left:8px;color:gray'>${esc(col.dataType ?: "")}</td>")
                if (col.description.isNotEmpty()) {
                    append("<td style='padding-left:8px'>${esc(col.description)}</td>")
                }
                append("</tr>")
            }
            append("</table>")
            if (node.columns.size > 15) append("<p><i>... and ${node.columns.size - 15} more columns</i></p>")
        }

        if (node.tags.isNotEmpty()) {
            append("<p style='margin-top:6px'><b>Tags:</b> ${node.tags.joinToString(", ") { "<code>${esc(it)}</code>" }}</p>")
        }

        append("<p style='color:gray;margin-top:6px'>${esc(node.originalFilePath)}</p>")
        append("</body></html>")
    }

    fun buildSourceDoc(source: DbtSource, index: ManifestIndex): String = buildString {
        append("<html><body style='margin:4px'>")

        append("<h3 style='margin:0 0 6px 0'>${esc(source.sourceName)}.${esc(source.name)}</h3>")
        append("<p><code>source</code> &middot; <i>${esc(source.packageName)}</i></p>")

        val parts = listOfNotNull(source.database, source.schema, source.identifier ?: source.name)
        if (parts.isNotEmpty()) {
            append("<p><code>${parts.joinToString(".") { esc(it) }}</code></p>")
        }

        if (source.description.isNotEmpty()) {
            append("<p style='margin:6px 0'>${esc(source.description)}</p>")
        }

        val downstream = index.getDownstream(source.uniqueId)
        if (downstream.isNotEmpty()) {
            append("<hr>")
            val names = downstream.mapNotNull { id -> friendlyName(id, index) }.take(8)
            append("<p><b>Used by (${downstream.size}):</b> ${names.joinToString(", ") { "<code>${esc(it)}</code>" }}")
            if (downstream.size > 8) append(" +${downstream.size - 8} more")
            append("</p>")
        }

        if (source.columns.isNotEmpty()) {
            append("<hr><p><b>Columns (${source.columns.size}):</b></p>")
            append("<table style='margin:2px 0'>")
            for ((_, col) in source.columns.entries.take(15)) {
                append("<tr>")
                append("<td><code>${esc(col.name)}</code></td>")
                append("<td style='padding-left:8px;color:gray'>${esc(col.dataType ?: "")}</td>")
                if (col.description.isNotEmpty()) {
                    append("<td style='padding-left:8px'>${esc(col.description)}</td>")
                }
                append("</tr>")
            }
            append("</table>")
            if (source.columns.size > 15) append("<p><i>... and ${source.columns.size - 15} more columns</i></p>")
        }

        if (source.tags.isNotEmpty()) {
            append("<p style='margin-top:6px'><b>Tags:</b> ${source.tags.joinToString(", ") { "<code>${esc(it)}</code>" }}</p>")
        }

        append("<p style='color:gray;margin-top:6px'>${esc(source.originalFilePath)}</p>")
        append("</body></html>")
    }

    fun buildMacroDoc(macro: DbtMacro): String = buildString {
        append("<html><body style='margin:4px'>")

        val argsStr = macro.arguments.joinToString(", ") { it.name }
        append("<h3 style='margin:0 0 6px 0'>${esc(macro.name)}($argsStr)</h3>")
        append("<p><code>macro</code> &middot; <i>${esc(macro.packageName)}</i></p>")

        if (macro.description.isNotEmpty()) {
            append("<p style='margin:6px 0'>${esc(macro.description)}</p>")
        }

        if (macro.arguments.isNotEmpty()) {
            append("<hr><p><b>Arguments:</b></p>")
            append("<table style='margin:2px 0'>")
            for (arg in macro.arguments) {
                append("<tr>")
                append("<td><code>${esc(arg.name)}</code></td>")
                if (arg.type != null) append("<td style='padding-left:8px;color:gray'>${esc(arg.type)}</td>")
                if (arg.description.isNotEmpty()) append("<td style='padding-left:8px'>${esc(arg.description)}</td>")
                append("</tr>")
            }
            append("</table>")
        }

        append("<p style='color:gray;margin-top:6px'>${esc(macro.originalFilePath)}</p>")
        append("</body></html>")
    }

    private fun friendlyName(uniqueId: String, index: ManifestIndex): String? =
        DbtUtils.friendlyName(uniqueId, index)

    private fun esc(s: String): String = DbtUtils.escapeHtml(s)
}
