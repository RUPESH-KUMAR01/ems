import * as SelectPrimitive from '@radix-ui/react-select'
import { cn } from '@/lib/cn'

export function Select({
  value,
  onValueChange,
  placeholder,
  options,
}: {
  value: string
  onValueChange: (v: string) => void
  placeholder?: string
  options: { value: string; label: string }[]
}) {
  return (
    <SelectPrimitive.Root value={value} onValueChange={onValueChange}>
      <SelectPrimitive.Trigger
        className="inline-flex w-full items-center justify-between rounded-md border border-border bg-surface px-3 py-2 text-sm"
      >
        <SelectPrimitive.Value placeholder={placeholder} />
        <SelectPrimitive.Icon>▾</SelectPrimitive.Icon>
      </SelectPrimitive.Trigger>
      <SelectPrimitive.Portal>
        <SelectPrimitive.Content className="z-50 rounded-md border border-border bg-surface shadow-lg">
          <SelectPrimitive.Viewport className="p-1">
            {options.map((o) => (
              <SelectPrimitive.Item
                key={o.value}
                value={o.value}
                className={cn(
                  'cursor-pointer rounded px-3 py-2 text-sm outline-none',
                  'data-[highlighted]:bg-paper',
                )}
              >
                <SelectPrimitive.ItemText>{o.label}</SelectPrimitive.ItemText>
              </SelectPrimitive.Item>
            ))}
          </SelectPrimitive.Viewport>
        </SelectPrimitive.Content>
      </SelectPrimitive.Portal>
    </SelectPrimitive.Root>
  )
}
