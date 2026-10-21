import type { ReactNode } from 'react'

export function DataTable<T>({
  rows,
  columns,
  keyFn,
  empty,
}: {
  rows: T[]
  columns: { header: string; cell: (row: T) => ReactNode }[]
  keyFn: (row: T) => string | number
  empty?: string
}) {
  if (!rows.length) {
    return <p className="text-sm text-ink-muted">{empty ?? 'No rows.'}</p>
  }
  return (
    <div className="overflow-x-auto rounded-lg border border-border">
      <table className="min-w-full text-left text-sm">
        <thead className="border-b border-border bg-paper/50">
          <tr>
            {columns.map((c) => (
              <th key={c.header} className="px-3 py-2 font-medium">
                {c.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr key={keyFn(row)} className="border-b border-border/60 last:border-0">
              {columns.map((c) => (
                <td key={c.header} className="px-3 py-2 align-top">
                  {c.cell(row)}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
