{{- define "projektagile.labels" -}}
app.kubernetes.io/name: projektagile
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end -}}
