import * as TabsPrimitive from '@radix-ui/react-tabs'
import { cn } from '@/lib/cn'

export function Tabs({
  tabs,
  defaultValue,
}: {
  tabs: { value: string; label: string; content: React.ReactNode }[]
  defaultValue?: string
}) {
  const initial = defaultValue ?? tabs[0]?.value ?? ''
  return (
    <TabsPrimitive.Root defaultValue={initial}>
      <TabsPrimitive.List className="flex gap-1 border-b border-border">
        {tabs.map((t) => (
          <TabsPrimitive.Trigger
            key={t.value}
            value={t.value}
            className={cn(
              'px-3 py-2 text-sm font-medium text-ink-muted',
              'data-[state=active]:border-b-2 data-[state=active]:border-stamp data-[state=active]:text-ink',
            )}
          >
            {t.label}
          </TabsPrimitive.Trigger>
        ))}
      </TabsPrimitive.List>
      {tabs.map((t) => (
        <TabsPrimitive.Content key={t.value} value={t.value} className="pt-4">
          {t.content}
        </TabsPrimitive.Content>
      ))}
    </TabsPrimitive.Root>
  )
}
